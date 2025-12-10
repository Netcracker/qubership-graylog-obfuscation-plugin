const PluginWebpackConfig = require('graylog-web-plugin').PluginWebpackConfig; // eslint-disable-line no-use-before-define
const loadBuildConfig = require('graylog-web-plugin').loadBuildConfig; // eslint-disable-line no-use-before-define
const path = require('path'); // eslint-disable-line no-use-before-define
const buildConfig = loadBuildConfig(path.resolve(__dirname, './build.config')); // eslint-disable-line no-use-before-define

module.exports = new PluginWebpackConfig('com.netcracker.graylog2.plugin.ObfuscationPlugin', buildConfig, {
    // Here goes your additional webpack configuration.
});

