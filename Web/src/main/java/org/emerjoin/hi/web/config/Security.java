package org.emerjoin.hi.web.config;

/**
 * @author Mario Junior.
 */
public class Security {

    private ContentSecurityPolicy contentPolicy = new ContentSecurityPolicy();

    public ContentSecurityPolicy getContentPolicy() {
        return contentPolicy;
    }

    public void setContentPolicy(ContentSecurityPolicy contentPolicy) {
        this.contentPolicy = contentPolicy;
    }

    public class ContentSecurityPolicy {

        private boolean denyIframeEmbeding = true;

        private boolean blockMixedContent = false;
        private String formActionDirective ="'none'";
        private String navigateToDirective ="*";

        private String reportingUrl="";

        private String defaultContentSource="* 'unsafe-inline'";
        private String scriptContentSource="* 'unsafe-inline' 'unsafe-eval'";
        private String objectContentSource="*";
        private String imageContentSource="*";
        private String mediaContentSource="*";
        private String styleContentSource="* 'unsafe-inline'";

        public boolean isDenyIframeEmbeding() {
            return denyIframeEmbeding;
        }

        public void setDenyIframeEmbeding(boolean denyIframeEmbeding) {
            this.denyIframeEmbeding = denyIframeEmbeding;
        }

        public String getReportingUrl() {
            return reportingUrl;
        }

        public void setReportingUrl(String reportingUrl) {
            this.reportingUrl = reportingUrl;
        }

        public String getDefaultContentSource() {
            return defaultContentSource;
        }

        public void setDefaultContentSource(String defaultContentSource) {
            this.defaultContentSource = defaultContentSource;
        }

        public String getScriptContentSource() {
            return scriptContentSource;
        }

        public void setScriptContentSource(String scriptContentSource) {
            this.scriptContentSource = scriptContentSource;
        }

        public String getObjectContentSource() {
            return objectContentSource;
        }

        public void setObjectContentSource(String objectContentSource) {
            this.objectContentSource = objectContentSource;
        }

        public String getImageContentSource() {
            return imageContentSource;
        }

        public void setImageContentSource(String imageContentSource) {
            this.imageContentSource = imageContentSource;
        }

        public String getMediaContentSource() {
            return mediaContentSource;
        }

        public void setMediaContentSource(String mediaContentSource) {
            this.mediaContentSource = mediaContentSource;
        }

        public String getStyleContentSource() {
            return styleContentSource;
        }

        public void setStyleContentSource(String styleContentSource) {
            this.styleContentSource = styleContentSource;
        }

        public boolean isBlockMixedContent() {
            return blockMixedContent;
        }

        public void setBlockMixedContent(boolean blockMixedContent) {
            this.blockMixedContent = blockMixedContent;
        }

        public String getNavigateToDirective() {
            return navigateToDirective;
        }

        public void setNavigateToDirective(String navigateToDirective) {
            this.navigateToDirective = navigateToDirective;
        }

        public String getFormActionDirective() {
            return formActionDirective;
        }

        public void setFormActionDirective(String formActionDirective) {
            this.formActionDirective = formActionDirective;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            String defaultSrc = this.getDefaultContentSource();
            if(!defaultSrc.isEmpty()) {
                builder.append("default-src ");builder.append(defaultSrc);builder.append("; ");
            }
            String scriptSrc = this.getScriptContentSource();
            if(!scriptSrc.isEmpty()){
                builder.append("script-src ");builder.append(scriptSrc);builder.append("; ");
                builder.append("script-src-attr ");builder.append(scriptSrc);builder.append("; ");
            }
            String styleSrc = this.getStyleContentSource();
            if(!styleSrc.isEmpty()){
                builder.append("style-src ");builder.append(styleSrc);builder.append("; ");
            }
            String imageSrc = this.getImageContentSource();
            if(!imageSrc.isEmpty()){
                builder.append("img-src ");builder.append(imageSrc);builder.append("; ");
            }
            String objectSrc = this.getObjectContentSource();
            if(!objectSrc.isEmpty()){
                builder.append("object-src ");builder.append(objectSrc);builder.append("; ");
            }
            String mediaContentSrc = this.getMediaContentSource();
            if(!mediaContentSrc.isEmpty()){
                builder.append("media-src "); builder.append(mediaContentSrc); builder.append("; ");
            }
            if(this.isBlockMixedContent())
                builder.append("block-all-mixed-content; ");

            String navigateToDirective = this.getNavigateToDirective();
            if(!navigateToDirective.isEmpty()){
                builder.append("navigate-to "); builder.append(navigateToDirective); builder.append("; ");
            }
            String formActionDirective = this.getFormActionDirective();
            if(!formActionDirective.isEmpty()){
                builder.append("form-action "); builder.append(formActionDirective); builder.append("; ");
            }
            if(!this.getReportingUrl().isEmpty()){
                builder.append("report-uri ");builder.append(this.getReportingUrl());
                builder.append("; report-to ");builder.append(this.getReportingUrl());builder.append("; ");
            }
            return builder.toString();
        }
    }

}
