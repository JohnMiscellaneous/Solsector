package soljars.gen.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import soljars.gen.utils.AstroCalc.KeplerComponent;

import java.util.ArrayList;
import java.util.List;

public class ThreeBodySolution {

    /**
     * Generate and spawn a three-body solution.
     *
     * @param system          the system to spawn into
     * @param parent          the math focus (barycenter anchor, usually a
     *                        custom invisible entity at the desired center)
     * @param period          natural period of the orbit in its source
     *                        units (the published T value)
     * @param gameDaysPerOrbit  how many in-game days = one natural period
     *                          (sets the pace of the dance)
     * @param worldScale      pixels per normalized unit (controls the
     *                        physical size of the dance)
     * @param planetIds       length-3 array of entity IDs
     * @param planetNames     length-3 array of display names
     * @param planetTypes     length-3 array of planet/star type strings
     * @param planetSizes     length-3 array of body radii
     * @param nIterations     integration sample count (= DFT length).
     *                        Higher = better fidelity to sharp features, but longer compute times.
     *                        2048 can do well for simple solutions, 
     *                        8192 is a good default for semi complex orbits. 
     *                        65536 will take forever but is necessary for the most complex
     *                        if your orbits somehow RESET?! increase samples.
     * @param kComponents     max harmonics retained per body. Higher =
     *                        sharper reconstruction. (jitterring = problem with this)
     * @param useVectorInputs true = full per-body vector input (positions
     *                        + velocities passed as positionInputs and
     *                        vectorInputs respectively).
     *                        false = symmetric position-based input;
     *                        positionInputs must be length-2 array (v1x, v1y)
     *                        and vectorInputs is ignored.
     * @param positionInputs  in VECTOR mode: 3x2 array of initial positions
     *                        per body. In POSITION mode: length-2 array
     *                        (v1x, v1y) for the symmetric velocity.
     * @param vectorInputs    in VECTOR mode: 3x2 array of initial velocities
     *                        per body. Ignored in POSITION mode.
     * @param isStar          true = spawn the three bodies as real stars
     */
    public static void generate(
            StarSystemAPI system,
            SectorEntityToken parent,
            double period,
            float gameDaysPerOrbit,
            float worldScale,
            String[] planetIds,
            String[] planetNames,
            String[] planetTypes,
            float[]  planetSizes,
            int nIterations,
            int kComponents,
            boolean useVectorInputs,
            double[][] positionInputs,
            double[][] vectorInputs,
            boolean isStar) {

        if (planetIds.length != 3 || planetNames.length != 3
                || planetTypes.length != 3 || planetSizes.length != 3) {
            throw new IllegalArgumentException(
                "ThreeBodyGen: planet arrays must each be length 3");
        }

        // --- Resolve IC ---
        double[][] r0 = new double[3][2];
        double[][] v0 = new double[3][2];

        if (useVectorInputs) {
            if (positionInputs.length != 3 || vectorInputs.length != 3) {
                throw new IllegalArgumentException(
                    "ThreeBodyGen VECTOR mode: position and vector inputs "
                    + "must each be 3x2");
            }
            for (int b = 0; b < 3; b++) {
                r0[b][0] = positionInputs[b][0];
                r0[b][1] = positionInputs[b][1];
                v0[b][0] = vectorInputs[b][0];
                v0[b][1] = vectorInputs[b][1];
            }
        } else {
            // Symmetric setup: positions hardcoded, velocity from (v1x, v1y)
            if (positionInputs.length < 1 || positionInputs[0].length != 2) {
                throw new IllegalArgumentException(
                    "ThreeBodyGen POSITION mode: positionInputs must be a "
                    + "1x2 (or larger) array; [0] = (v1x, v1y)");
            }
            double v1x = positionInputs[0][0];
            double v1y = positionInputs[0][1];
            r0[0][0] = -1.0; r0[0][1] = 0.0;
            r0[1][0] =  1.0; r0[1][1] = 0.0;
            r0[2][0] =  0.0; r0[2][1] = 0.0;
            v0[0][0] =  v1x; v0[0][1] =  v1y;
            v0[1][0] =  v1x; v0[1][1] =  v1y;
            v0[2][0] = -2.0 * v1x; v0[2][1] = -2.0 * v1y;
        }

        // Recenter on center of mass so the dance is anchored to parent
        double comX = (r0[0][0] + r0[1][0] + r0[2][0]) / 3.0;
        double comY = (r0[0][1] + r0[1][1] + r0[2][1]) / 3.0;
        for (int b = 0; b < 3; b++) {
            r0[b][0] -= comX;
            r0[b][1] -= comY;
        }

        // --- Integrate one period ---
        double dt = period / (double) nIterations;
        double[][] trajX = new double[3][nIterations];
        double[][] trajY = new double[3][nIterations];
        integrate(r0, v0, trajX, trajY, nIterations, dt);

        // --- Per-body Fourier decomposition → CompoundOrbit ---
        for (int b = 0; b < 3; b++) {
            double[] re = new double[nIterations];
            double[] im = new double[nIterations];
            dft(trajX[b], trajY[b], re, im, nIterations);

            double[] amp = new double[nIterations];
            double[] phase = new double[nIterations];
            int[] signedFreq = new int[nIterations];
            for (int n = 0; n < nIterations; n++) {
                signedFreq[n] = (n <= nIterations / 2) ? n : n - nIterations;
                amp[n] = Math.sqrt(re[n]*re[n] + im[n]*im[n]);
                phase[n] = Math.atan2(im[n], re[n]);
            }

            boolean[] used = new boolean[nIterations];
            used[0] = true;     // DC handled separately

            List<KeplerComponent> comps =
                new ArrayList<KeplerComponent>(kComponents + 1);

            // DC component = body's mean position offset
            double dcAmp = Math.sqrt(re[0]*re[0] + im[0]*im[0]);
            double dcPhase = Math.atan2(im[0], re[0]);
            if (dcAmp > 1e-10) {
                float dcSma      = (float)(dcAmp * worldScale);
                float dcLongPeri = (float) Math.toDegrees(dcPhase);
                // 1e7-day period → effectively static during gameplay
                comps.add(CompoundOrbitTool.kc(
                    dcSma, 0f, dcLongPeri, 1e7f, 0f, +1f));
            }

            // Top-K harmonics by amplitude
            for (int pick = 0; pick < kComponents; pick++) {
                int best = -1;
                double bestAmp = -1.0;
                for (int n = 0; n < nIterations; n++) {
                    if (!used[n] && amp[n] > bestAmp) {
                        bestAmp = amp[n];
                        best = n;
                    }
                }
                if (best < 0 || bestAmp < 1e-10) break;
                used[best] = true;

                int sf = signedFreq[best];
                if (sf == 0) continue;

                float smaPixels   = (float)(amp[best] * worldScale);
                float longPeriDeg = (float) Math.toDegrees(phase[best]);
                // CCW (sf>0) needs negative period; CW (sf<0) positive.
                float per = (sf > 0)
                    ? -gameDaysPerOrbit / (float) sf
                    :  gameDaysPerOrbit / (float)(-sf);

                comps.add(CompoundOrbitTool.kc(
                    smaPixels, 0f, longPeriDeg, per, 0f, +1f));
            }

            PlanetAPI p;
            if (isStar) {
                // Spawn as real star with appropriate role flag.
                if (b == 0) {
                    // Primary — initStar handles this naturally
                    p = system.initStar(
                        planetIds[b], planetTypes[b], planetSizes[b],
                        planetSizes[b] * 0.75f,  // corona
                        1f, 0.2f, 3f);
                    p.setName(planetNames[b]);
                } else {
                    // Secondary / tertiary — addPlanet with star type, then flag
                    p = (PlanetAPI) system.addPlanet(
                        planetIds[b], parent, planetNames[b], planetTypes[b],
                        0f, planetSizes[b], 500f, gameDaysPerOrbit);
                    if (b == 1) {
                        system.setSecondary(p);
                    } else {
                        system.setTertiary(p);
                    }
                    system.addCorona(p, planetSizes[b] * 0.75f, 3f, 0.2f, 1f);
                }
            } else {
                // Plain planets — no star role, no corona
                p = (PlanetAPI) system.addPlanet(
                    planetIds[b], parent, planetNames[b], planetTypes[b],
                    0f, planetSizes[b], 500f, gameDaysPerOrbit);
            }
            
            KeplerComponent[] arr = (KeplerComponent[]) comps.toArray(
                new KeplerComponent[comps.size()]);
            CompoundOrbitTool.attach(p, parent, arr);
        }
    }

    // ====================================================================
    // Yoshida 4th-order symplectic integrator
    // ====================================================================

    private static void accel(double[][] pos, double[][] outA) {
        double EPS2 = 1e-9;
        for (int i = 0; i < 3; i++) { outA[i][0] = 0.0; outA[i][1] = 0.0; }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) continue;
                double dx = pos[j][0] - pos[i][0];
                double dy = pos[j][1] - pos[i][1];
                double r2 = dx*dx + dy*dy + EPS2;
                double inv_r3 = 1.0 / (r2 * Math.sqrt(r2));
                outA[i][0] += dx * inv_r3;
                outA[i][1] += dy * inv_r3;
            }
        }
    }

    private static void integrate(double[][] r0, double[][] v0,
                                  double[][] trajX, double[][] trajY,
                                  int N, double dt) {
        double[][] r = new double[3][2];
        double[][] v = new double[3][2];
        for (int b = 0; b < 3; b++) {
            r[b][0] = r0[b][0]; r[b][1] = r0[b][1];
            v[b][0] = v0[b][0]; v[b][1] = v0[b][1];
        }

        // Yoshida coefficients
        double w1 = 1.0 / (2.0 - Math.pow(2.0, 1.0/3.0));
        double w0 = 1.0 - 2.0 * w1;
        double[] c = { w1 * 0.5, (w0 + w1) * 0.5, (w0 + w1) * 0.5, w1 * 0.5 };
        double[] d = { w1, w0, w1, 0.0 };

        int SUBSTEPS = 8;
        double sdt = dt / (double) SUBSTEPS;

        double[][] a = new double[3][2];

        for (int step = 0; step < N; step++) {
            for (int b = 0; b < 3; b++) {
                trajX[b][step] = r[b][0];
                trajY[b][step] = r[b][1];
            }

            for (int sub = 0; sub < SUBSTEPS; sub++) {
                for (int s = 0; s < 4; s++) {
                    for (int b = 0; b < 3; b++) {
                        r[b][0] += c[s] * sdt * v[b][0];
                        r[b][1] += c[s] * sdt * v[b][1];
                    }
                    if (d[s] != 0.0) {
                        accel(r, a);
                        for (int b = 0; b < 3; b++) {
                            v[b][0] += d[s] * sdt * a[b][0];
                            v[b][1] += d[s] * sdt * a[b][1];
                        }
                    }
                }
            }
        }
    }

    private static void dft(double[] xs, double[] ys,
                            double[] re, double[] im, int N) {
        for (int n = 0; n < N; n++) {
            double sumRe = 0.0, sumIm = 0.0;
            for (int k = 0; k < N; k++) {
                double angle = -2.0 * Math.PI * n * k / N;
                double cs = Math.cos(angle), sn = Math.sin(angle);
                sumRe += xs[k] * cs - ys[k] * sn;
                sumIm += xs[k] * sn + ys[k] * cs;
            }
            re[n] = sumRe / N;
            im[n] = sumIm / N;
        }
    }
}