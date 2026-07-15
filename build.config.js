const path = require("path");

module.exports = {
  web_src_path: path.resolve(
    __dirname,
    process.env.GRAYLOG_WEB_SRC ||
      ".graylog/graylog2-server/graylog2-web-interface",
  ),
};
