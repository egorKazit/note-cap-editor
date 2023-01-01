class ThreadWrapper {

    constructor() {
        this.threadSelect = document.getElementById("thread-list");
        this.threadSelect.addEventListener("change", function (event) {
            threadWrapper.switchToThread(event.target.value);
        })
        this.newThreadModal = new bootstrap.Modal(document.getElementById("new-thread"), {
            backdrop: 'static', keyboard: false
        });
        this.threadModalCloseButton = document.getElementById("new-thread-close");
    }

    async loadThreads() {
        const threadResponse = await fetch('/threads');
        const threadsInJson = await threadResponse.json();
        threadsInJson.forEach(thread => {
            this.addThreadToList(thread);
        })
        let separatedPath = window.location.pathname.split("/");
        let uuid = separatedPath[separatedPath.length - 1];
        let selectedItem = Array.from(this.threadSelect.options).find(item => item.value === uuid);
        if (selectedItem !== undefined && selectedItem !== null) {
            selectedItem.selected = true;
        }
        return threadsInJson.length > 0;
    }

    async createThread() {
        let newThreadName = document.getElementById("new-thread-name");
        const response = await fetch('/thread',
            {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({'name': newThreadName.value})
            });
        if (response.status !== 200) {
            threadWrapper.newThreadModal.hide();
            modalPopup.callPopup("Thread", "Thread can not be created", false, () => {

            });
        }
        let thread = await response.json();
        this.switchToThread(thread.ID);
    }

    requestNewThread() {
        this.threadModalCloseButton.disabled = true;
        this.newThreadModal.show();
    }

    openNewThread() {
        this.threadModalCloseButton.disabled = false;
        this.newThreadModal.show();
    }

    switchToThread(uuid) {
        window.location.replace('/thread/' + uuid);
    }

    async removeThread() {
        modalPopup.callPopup("Thread", "Do you really need to delete the thread?", true, async () => {
            let separatedPath = window.location.pathname.split("/");
            let uuid = separatedPath[separatedPath.length - 1];
            const response = await fetch('/thread/' + uuid,
                {
                    method: 'DELETE'
                });
            if (response.status === 200) {
                modalPopup.callPopup("Thread", "Thread was deleted", false, () => {
                    window.location.replace('/');
                })
            }
        })
    }

    addThreadToList(thread) {
        let threadOption = document.createElement('option');
        threadOption.value = thread.ID;
        threadOption.innerHTML = thread.name;
        if (this.threadSelect === null) {
            this.threadSelect = document.getElementById("thread-list");
        }
        this.threadSelect.appendChild(threadOption);
        threadOption.selected = true;
    }

    getSelectedThread() {
        return this.threadSelect.value;
    }

}

let threadWrapper = new ThreadWrapper();