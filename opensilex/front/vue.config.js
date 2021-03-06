const UglifyJsPlugin = require('uglifyjs-webpack-plugin');

module.exports = {
    configureWebpack: {
        externals: {
            'vue': 'Vue',
            'core-js': 'core-js',
            'vue-class-component': 'vue-class-component',
            'vue-property-decorator': 'vue-property-decorator',
            'vue-router': 'vue-router',
            'vuex': 'vuex',
            'node-fetch': 'node-fetch',
        },
        performance: {
            hints: false
        },
        optimization: {
            minimizer: [
                new UglifyJsPlugin({
                    uglifyOptions: {
                        output: {
                            comments: false
                        }
                    }
                })
            ]
        }
    }
};