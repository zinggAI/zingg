package zingg.common.client.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BannerService {
    private static final String PRODUCT_NAME = "Zingg AI";
    private static final String PRODUCT_VERSION = "0.6.0";
    private static final Log LOG = LogFactory.getLog(BannerService.class);


    public static void printBanner(boolean collectMetrics) {
        LOG.info("");
        LOG.info("**************************************************************************");
        LOG.info("*                                                                        *");
        LOG.info("*                                "+getProductName()+"                                *");
        LOG.info("*                        (C) 2021 Zingg Labs, Inc.                       *");
        LOG.info("*                                                                        *");
        LOG.info("*                          https://www.zingg.ai/                         *");
        LOG.info("*                                                                        *");
        LOG.info("*                        using: Zingg v"+getProductVersion()+"                             *");
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

    public static String getProductName() {
        return PRODUCT_NAME;
    }

    public static String getProductVersion() {
        return PRODUCT_VERSION;
    }
}
