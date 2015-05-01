package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.HajpPlugin;
import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.exceptions.HajpClusterConfigurationException;

import hudson.Extension;
import hudson.model.ManagementLink;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.io.IOException;

/**
 * The Class HAJPSettingsPage.
 * <p/>
 * This Class extends the {@link hudson.model.ManagementLink} which makes it available in
 * Jenkins' manage page. The HAJP settings page displays information about the
 * cluster, and the user can also perform various actions from this page, such
 * as safely restart Jenkins, push plugins to other Jenkinses, and choose what
 * to replicate from the global Jenkins configurations.
 */
@Log4j2 @Extension @ExportedBean public class HajpSettingsPage extends ManagementLink {

    public static final String CLS_MBR_CFG_ERR =
        "Jenkins FrontEnd configuration does not contain a self or orchestrator key";

    /**
     * The SLF4J logger
     */
    private static final org.slf4j.Logger logger =
        LoggerFactory.getLogger(HajpSettingsPage.class.getName());

    /**
     * The plugin list map.
     */

    //private HajpOperationFacade operationFacade;
    public HajpSettingsPage() {
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.model.ManagementLink#getIconFileName()
     */
    @Override public String getIconFileName() {
        return "gear2.png";
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.model.Action#getDisplayName()
     */
    @Override public String getDisplayName() {
        return "HAJP Settings";
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.model.ManagementLink#getUrlName()
     */
    @Override public String getUrlName() {
        return "HAJPconfig";
    }

    /**
     * Simple master status method used on Settings page
     * @return Active Master, Hot standby or empty if not.
     */
    public String getHajpMasterStatus() {
        if (HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()) {
            return Messages.cluster_activemaster();
        }
        if (HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()) {
            return Messages.cluster_hotstandby();
        } else {
            return "";
        }
    }

    /**
     * Simple get status method used on Settings page.
     * @return JOINED or DISCONNECTED.
     */
    public String getHajpClusterStatus() {
        if (HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()) {
            return Messages.cluster_joined();
        }
        return Messages.cluster_disconnected();
    }

    /**
     * Cluster configuration front-end returns a json with self and orchestrator fields
     * which this method processes and launches cluster
     * @param req StaplerRequest object
     * @param rsp StaplerResponse object
     * @return HttpResponse
     * @throws ServletException
     * @throws NumberFormatException
     * @throws IOException
     */
    public HttpResponse doSubmitClusterMembers(StaplerRequest req, StaplerResponse rsp)
        throws ServletException, NumberFormatException, IOException {

        JSONObject json = req.getSubmittedForm();

        Object o = json.get("clusterMembers");
        if (o instanceof JSONObject) {
            JSONObject member = (JSONObject) o;
            JSONArray members = new JSONArray();
            members.add(member);
            json.put("clusterMembers", members);
        }
        try {
            if (json.containsKey("self") && json.containsKey("orchestrator")) {
                String ownIp = (String) json.getJSONObject("self").get("ip");
                Integer ownPort = Integer.parseInt((String) json.getJSONObject("self").get("port"));
                String orchestratorIp = (String) json.getJSONObject("orchestrator").get("ip");
                Integer orchestratorPort =
                    Integer.parseInt((String) json.getJSONObject("orchestrator").get("port"));
                HajpPlugin.getHajpCluster()
                    .launch(ownIp, ownPort, orchestratorIp, orchestratorPort);
            } else {
                throw new HajpClusterConfigurationException(CLS_MBR_CFG_ERR);
            }
        } catch (NumberFormatException | HajpClusterConfigurationException ex) {
            log.warn(ex.getMessage());
            return HttpResponses.error(ex);
        }

        return HttpResponses.redirectViaContextPath("HAJPconfig");
    }
}
