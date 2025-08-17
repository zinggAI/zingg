package zingg.common.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BannerPrinter {

    private static final Log LOG = LogFactory.getLog(BannerPrinter.class);

    public void print(String productName, String version, boolean collectMetrics) {
        LOG.info("");
        LOG.info("**************************************************************************");
        LOG.info("*                                                                        *");
        LOG.info("*                                "+productName+"                                *");
        LOG.info("*                        (C) 2021 Zingg Labs, Inc.                       *");
        LOG.info("*                                                                        *");
        LOG.info("*                          https://www.zingg.ai/                         *");
        LOG.info("*                                                                        *");
        LOG.info("*                        using: Zingg v"+version+"                             *");
        LOG.info("*                                                                        *");
        if(collectMetrics) {
            LOG.info("*            ** Note about analytics collection by Zingg AI **           *");
            LOG.info("*                                                                        *");
            LOG.info("*  Please note that Zingg captures a few metrics about application's     *");
            LOG.info("*  runtime parameters. However, no personal data or application data     *");
            LOG.info("*  is captured. If you want to switch off this feature, please set the   *");
            LOG.info("*  flag collectMetrics to false in config. For details, please refer to  *");
            LOG.info("*  the Zingg docs (https://docs.zingg.ai/zingg/security).                *");
            LOG.info("*                                                                        *");
            LOG.info("**************************************************************************");
            LOG.info("");
        }
        else {
            LOG.info("*  Zingg is not collecting any analytics data and will only log a blank  *");
            LOG.info("*                    event with name of the phase.                       *");
            LOG.info("*                                                                        *");
            LOG.info("**************************************************************************");
            LOG.info("");
        }
    }
}
