package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LargeMergeTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(LargeMergeTest.class);


    private ContextNetwork network2 = null;
    public void testLoadSampleMerge(String filename, int _sample_count) throws Exception {

        assert _sample_count > 1;

        DataReader dReader = new DataReader();

        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);

        // NetworkBuildingHelper helper = new NetworkBuildingHelper(network1);

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = NetworkBuildingHelper.createNetworkForEachInstace(network1);

        logger.info("Creating Samples... " + _sample_count);
        //Generate samples
        List<ContextNetwork> samples = NetworkBuildingHelper.sample(network1, _sample_count);
//
        for (ContextNetwork sa: samples) logger.info("Nodes in merge graph " + sa.nodeCount());

        for (ContextNetwork sample : samples) {
            if ( !NetworkBuildingHelper.validateSample(network1, sample) )
                logger.info("Invalid Sample");
        }

        ContextNetwork merge = samples.get(0);
        for (int i=1; i<samples.size(); i++) {
            logger.info("Merging Sample #" + i);
            merge.merge(samples.get(i));
            logger.info("Post merge node count #" + merge.nodeCount());
            if ( !NetworkBuildingHelper.validateSample(network1, merge) )
                logger.info("Merge corrupted merge graph");
        }

        logger.info("Merging Instance Nets");
        for (ContextNetwork instanceNet : instanceNets)
            merge.merge(instanceNet);
        logger.info("Merges Complete");

        if ( !NetworkBuildingHelper.validateSample(network1, merge) )
            logger.info("Merge corrupted merge graph");

        logger.info("checkorder: " + NetworkBuildingHelper.checkOrderStrict(merge));

        if (network2 == null) {
            logger.info("Loading network2... ");
            network2 = dReader.readInstanceGraphs(filename);
        }

        logger.info("Final node counts: " + merge.nodeCount() + " " + network2.nodeCount());

        boolean val = merge.compareNetwork(network2);
        logger.info("Equals: " + val);
        Assert.assertEquals(val, true);
    }

    @Test
    public void checkOrderTest() throws Exception {
        DataReader dReader = new DataReader();
        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");
        logger.info("strict check " + NetworkBuildingHelper.checkOrderStrict(network1));
    }

    @Test
    public void testSmall() throws Exception {
        testLoadSampleMerge("/data/osm/inst-small.sim", 10);
    }

    @Test
    public void testMid() throws Exception {
        //testLoadSampleMerge("/data/osm/inst-mid.sim", 5);
        testLoadSampleMerge("/data/osm/instance.sim.4", 5);
    }

    @Test
    public void repeatTestMid() throws Exception {
        for (int i=1; i<3; i++) {
            testLoadSampleMerge("/data/osm/instance.sim.4", 5*i);
            logger.info("------------------------------------------");
        }
    }

    @Test
    public void testLarge() throws Exception {
        testLoadSampleMerge("/data/osm/inst-large.sim", 5);
    }

}
