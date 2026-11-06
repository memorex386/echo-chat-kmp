// Compose fetches strings, icons and fonts from composeResources/. Karma serves the test page but
// not those files unless it's told to, so the shared UI tests found no text in a browser. As
// Compose's own resources library does, serve the test build's resource files. The tests fetch them
// where Karma serves them, under /base/kotlin/ (loadStrings in core:testing).
(function (config) {
  const path = require("path");
  const resources = path.resolve(config.basePath, "kotlin", "composeResources");
  config.files = [
    { pattern: path.join(resources, "**", "*"), included: false, served: true, watched: false },
  ].concat(config.files);
})(config);
