async function onLoad() {
    await setThreadOrRequestNewOne();
}

async function setThreadOrRequestNewOne() {
    if (!await threadWrapper.loadThreads()) {
        threadWrapper.requestNewThread();
    }
}
