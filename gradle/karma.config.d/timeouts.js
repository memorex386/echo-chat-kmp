// Headless Chrome can be slow to start when several modules run their browser tests at once, or
// on a busy CI runner, so Karma waits up to 2 minutes for it rather than 30 seconds.
config.set({
  captureTimeout: 120000,
  browserNoActivityTimeout: 120000,
  browserDisconnectTimeout: 120000,
});

// Each test may take up to 15 seconds rather than Mocha's 2. On a busy CI runner, a UI test that
// opens a photo took longer than 2 seconds (PHOTO-05). Set directly, as config.set would replace
// the whole client section.
config.client = config.client || {};
config.client.mocha = Object.assign({}, config.client.mocha, { timeout: 15000 });
