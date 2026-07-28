Runcode import soljars.gen.systems.sol.RemnantHordeGen;
StarSystemAPI sys = (StarSystemAPI)$playerFleet.getContainingLocation();
new RemnantHordeGen().generate(sys);