package org.jenkinsci.plugins.codesonar.conditions;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.Result;
import java.util.List;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.codesonar.CodeSonarBuildAction;
import org.jenkinsci.plugins.codesonar.models.CodeSonarBuildActionDTO;
import org.jenkinsci.plugins.codesonar.models.Metric;
import org.jenkinsci.plugins.codesonar.models.procedures.ProcedureRow;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.model.Run;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

/**
 *
 * @author Andrius
 */
public class ProcedureCyclomaticComplexityExceededCondition extends Condition {

    private static final String NAME = "Cyclomatic complexity";

    private int maxCyclomaticComplexity = 30;
    private String warrantedResult = Result.UNSTABLE.toString();

    @DataBoundConstructor
    public ProcedureCyclomaticComplexityExceededCondition(int maxCyclomaticComplexity) {
        this.maxCyclomaticComplexity = maxCyclomaticComplexity;
    }

    @Override
    public Result validate(Run<?, ?> run, Launcher launcher, TaskListener listener) throws AbortException {
        CodeSonarBuildAction buildAction = run.getAction(CodeSonarBuildAction.class);
        if (buildAction == null) {
            return Result.SUCCESS;
        }

        CodeSonarBuildActionDTO buildActionDTO = buildAction.getBuildActionDTO();
        if (buildActionDTO == null) {
            return Result.SUCCESS;
        }

        List<ProcedureRow> procedureRows = buildActionDTO.getProcedures().getProcedureRows();
        for (ProcedureRow procedureRow : procedureRows) {
            Metric cyclomaticComplexityMetric = procedureRow.getMetricByName("Cyclomatic Complexity");

            String value = cyclomaticComplexityMetric.getValue();
            if (Integer.parseInt(value) > maxCyclomaticComplexity) {
                Result result = Result.fromString(warrantedResult);
                return result;
            }
        }

        return Result.SUCCESS;
    }

    public int getMaxCyclomaticComplexity() {
        return maxCyclomaticComplexity;
    }

    @DataBoundSetter
    public void setMaxCyclomaticComplexity(int maxCyclomaticComplexity) {
        this.maxCyclomaticComplexity = maxCyclomaticComplexity;
    }

    public String getWarrantedResult() {
        return warrantedResult;
    }

    @DataBoundSetter
    public void setWarrantedResult(String warrantedResult) {
        this.warrantedResult = warrantedResult;
    }
    
    @Symbol("cyclomaticComplexity")
    @Extension
    public static final class DescriptorImpl extends ConditionDescriptor<ProcedureCyclomaticComplexityExceededCondition> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }

        public FormValidation doCheckMaxCyclomaticComplexity(@QueryParameter("maxCyclomaticComplexity") int maxCyclomaticComplexity) {
            if (maxCyclomaticComplexity < 0) {
                return FormValidation.error("Cannot be a negative number");
            }

            return FormValidation.ok();
        }
    }
}
