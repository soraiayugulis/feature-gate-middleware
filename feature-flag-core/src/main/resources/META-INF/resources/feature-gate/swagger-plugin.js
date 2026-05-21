(function() {
    'use strict';

    /**
     * Swagger UI plugin to render feature flag badges.
     * Reads x-feature-flags extension on each operation and renders badges.
     */
    const FeatureFlagPlugin = function(system) {
        return {
            statePlugins: {
                spec: {
                    wrapActions: {
                        updateOperation: function(ori, system) {
                            return function(action, context) {
                                const operation = context.operation;
                                const xFeatureFlags = operation.get('x-feature-flags');
                                
                                if (xFeatureFlags && Array.isArray(xFeatureFlags)) {
                                    const badges = xFeatureFlags.map(flag => {
                                        const key = flag.key || 'unknown';
                                        const description = flag.description || '';
                                        return `<span class="feature-flag-badge" title="${description}">⚑ ${key}</span>`;
                                    }).join(' ');
                                    
                                    // Inject badges into the operation description
                                    const currentDescription = operation.get('description') || '';
                                    operation.set('description', `${badges}<br/>${currentDescription}`);
                                }
                                
                                return ori(action, context);
                            };
                        }
                    }
                }
            }
        };
    };

    if (window.SwaggerUIBundle) {
        window.SwaggerUIBundle.plugins.FeatureFlagPlugin = FeatureFlagPlugin;
    }
})();
