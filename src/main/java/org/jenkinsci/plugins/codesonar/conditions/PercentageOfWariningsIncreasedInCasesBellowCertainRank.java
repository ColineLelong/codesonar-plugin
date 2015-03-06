package org.jenkinsci.plugins.codesonar.conditions;

import static com.google.common.base.Preconditions.*;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.jenkinsci.plugins.codesonar.CodeSonarBuildAction;
import org.jenkinsci.plugins.codesonar.models.Analysis;
import org.jenkinsci.plugins.codesonar.models.Warning;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 *
 * @author andrius
 */
public class PercentageOfWariningsIncreasedInCasesBellowCertainRank extends Condition {

    private static final String NAME = "Warning percentage below a certain rank";

    private int rankOfWarnings;
    private float warningPercentage;
    private String warrantedResult = Result.UNSTABLE.toString();

    @DataBoundConstructor
    public PercentageOfWariningsIncreasedInCasesBellowCertainRank(int rankOfWarnings, float warningPercentage) {
        this.rankOfWarnings = rankOfWarnings;
        this.warningPercentage = warningPercentage;
    }

    @Override
    public Result validate(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        CodeSonarBuildAction buildAction = build.getAction(CodeSonarBuildAction.class);
        if (buildAction == null) {
            return Result.SUCCESS;
        }

        Analysis analysis = buildAction.getAnalysis();
        if (analysis == null) {
            return Result.SUCCESS;
        }

        int totalNumberOfWarnings = analysis.getWarnings().size();

        float severeWarnings = 0.0f;
        for (Warning warning : analysis.getWarnings()) {
            if (warning.getRank() < rankOfWarnings) {
                severeWarnings++;
            }
        }

        float calculatedWarningPercentage = (severeWarnings / totalNumberOfWarnings) * 100;

        System.out.println("----------------calculatedWarningPercentage----------------------");
        System.out.println(calculatedWarningPercentage);
        System.out.println("--------------------------------------");

        if (calculatedWarningPercentage > warningPercentage) {
            Result result = Result.fromString(warrantedResult);
            return result;
        }

        return Result.SUCCESS;
    }

    public int getRankOfWarnings() {
        return rankOfWarnings;
    }

    public void setRankOfWarnings(int rankOfWarnings) {
        this.rankOfWarnings = rankOfWarnings;
    }

    public float getWarningPercentage() {
        return warningPercentage;
    }

    public void setWarningPercentage(float warningPercentage) {
        this.warningPercentage = warningPercentage;
    }

    public String getWarrantedResult() {
        return warrantedResult;
    }

    @DataBoundSetter
    public void setWarrantedResult(String warrantedResult) {
        this.warrantedResult = warrantedResult;
    }

    @Extension
    public static final class DescriptorImpl extends ConditionDescriptor<PercentageOfWariningsIncreasedInCasesBellowCertainRank> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return NAME;
        }
    }
}