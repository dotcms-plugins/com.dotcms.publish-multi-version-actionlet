package com.dotmarketing.osgi.actionlet;

import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.factories.PublishFactory;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.htmlpageasset.model.HTMLPageAsset;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.workflows.actionlet.Actionlet;
import com.dotmarketing.portlets.workflows.actionlet.PublishContentActionlet;
import com.dotmarketing.portlets.workflows.actionlet.WorkFlowActionlet;
import com.dotmarketing.portlets.workflows.model.WorkflowActionClassParameter;
import com.dotmarketing.portlets.workflows.model.WorkflowActionFailureException;
import com.dotmarketing.portlets.workflows.model.WorkflowActionletParameter;
import com.dotmarketing.portlets.workflows.model.WorkflowProcessor;
import com.dotmarketing.portlets.workflows.model.WorkflowStep;
import com.dotmarketing.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the Actionlet for Publishing content to all versions.
 */
@Actionlet(publish = true)
public class PublishAllContentsActionlet extends WorkFlowActionlet {

    private static final long serialVersionUID = 1L;

    public String getName() {
        return "Publish All Versions";
    }

    public String getHowTo() {
        return "This actionlet will publish all version contents.";
    }

    public WorkflowStep getNextStep() {
        return null;
    }

    @Override
    public List<WorkflowActionletParameter> getParameters() {
        return null;
    }

    public void executeAction(final WorkflowProcessor processor,
                              final Map<String, WorkflowActionClassParameter> params) throws WorkflowActionFailureException {

        try {

            final Contentlet contentlet = processor.getContentlet();

            if (processor.getContentlet().isArchived()) {
                APILocator.getContentletAPI().unarchive(processor.getContentlet(), processor.getUser(), false);
            }

            final boolean respectFrontendRoles = null != processor.getContentletDependencies()?
                    processor.getContentletDependencies().isRespectAnonymousPermissions(): false;

            final List<Contentlet> contentletVersions = APILocator.getContentletAPI()
                    .getAllLanguages(contentlet, false, processor.getUser(), respectFrontendRoles);

            for (final Contentlet contentletVersion : contentletVersions) {

                this.setIndexPolicy(contentlet, contentletVersion);
                contentlet.setProperty(Contentlet.WORKFLOW_IN_PROGRESS, Boolean.TRUE);
                APILocator.getContentletAPI().publish(contentletVersion, processor.getUser(),
                        respectFrontendRoles);
            }
        } catch (Exception e) {

            Logger.error(PublishContentActionlet.class, e.getMessage(), e);
            throw new WorkflowActionFailureException(e.getMessage(),e);
        }
    } // executeAction.

    private void setIndexPolicy (final Contentlet originContentlet, final Contentlet newContentlet) {

        newContentlet.setIndexPolicy(originContentlet.getIndexPolicy());
        newContentlet.setIndexPolicyDependencies(originContentlet.getIndexPolicyDependencies());
    }
} // E:O:F:PublishContentActionlet.
