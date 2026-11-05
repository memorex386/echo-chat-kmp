// Chrome's sandbox can't start on some Linux CI machines, so on CI headless Chrome runs without it.
// The tests only load this project's own pages.
if (process.env.CI) {
  config.customLaunchers = Object.assign({}, config.customLaunchers, {
    ChromeHeadlessCI: { base: "ChromeHeadless", flags: ["--no-sandbox"] },
  });
  config.browsers = ["ChromeHeadlessCI"];
}
