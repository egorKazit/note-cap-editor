class ExistingFiles {

    constructor() {
        this.existingFiles = document.getElementById("existing-files-accordion");
    }

    createNewThreadItem(threadItem) {
        let newNote = document.getElementById("existing-files-draft").cloneNode(true);
        newNote.innerHTML = newNote.innerHTML
            .replaceAll("existing-file-draft", "existing-file-" + threadItem.ID)
            .replaceAll("collapse-item-draft", "collapse-item-" + threadItem.ID)
            .replaceAll("collapse-item-card-body-draft", "collapse-item-card-body-" + threadItem.ID)
            .replaceAll("existing-created-file-draft-uuid", threadItem.ID)
            .replaceAll("new-file-content", threadItem.content)
            .replaceAll("existing-file-edit-button-draft", "existing-file-edit-button-" + threadItem.ID)
            .replaceAll("existing-file-download-button-draft", "existing-file-download-button-" + threadItem.ID)
            .replaceAll("collapse-item-text-draft", threadItem.ID)
            .replaceAll("existing-created-file-draft-type", threadItem.type);
        newNote.hidden = false;
        if (existingFiles.existingFiles.children.length > 0) {
            existingFiles.existingFiles.insertBefore(newNote, existingFiles.existingFiles.children[0]);
        } else {
            existingFiles.existingFiles.append(newNote);
        }
        if (threadItem.type === 'NOTE') {
            document.getElementById("existing-file-download-button-" + threadItem.ID).hidden = true;
        } else if (threadItem.type === 'ATTACHMENT') {
            document.getElementById("existing-file-edit-button-" + threadItem.ID).hidden = true;
        }
    }

    edit(id) {
        if (nodeForm.entityValue.value !== '' &&
            nodeForm.entityValue.value !== id) {
            modalPopup.callPopup("File edit", "Are you agree to replace working area with note content", true, () => {
                existingFiles.setContent(id)
            });
        } else {
            this.setContent(id);
        }
        return true;
    }

    setContent(id) {
        let collapsedBody = document.getElementById("collapse-item-card-body-" + id);
        nodeForm.entityValue.value = collapsedBody.outerText;
        if (nodeForm.entityValue.value !== null && nodeForm.entityValue.value !== "") {
            nodeForm.entitySaveButton.disabled = false;
        }
        nodeForm.entityName.value = id;
    }

    download(id) {
        fetch("/thread/item/" + id)
            .then((response) => {
                if (response.status === 200) {
                    return response.json();
                }
            }).then((item) => {
            if (item.type === 'ATTACHMENT') {
                fetch("/thread/attachment/" + id)
                    .then((res) => {
                        return res.blob();
                    })
                    .then((data) => {
                        let hyperlink = document.createElement("a");
                        hyperlink.href = window.URL.createObjectURL(data);
                        hyperlink.download = item.name.split("/")[item.name.split("/").length - 1];
                        hyperlink.click();
                    });
            }
        });
    }

    async remove(id, type) {
        let collapsedBody = document.getElementById("existing-file-" + id);
        let response = await fetch(type === "NOTE" ? "/thread/note/" + id : "/thread/attachment/" + id, {
            method: "DELETE",
            body: "{}"
        });
        if (response.status === 200 || response.status === 201) {
            collapsedBody.parentElement.remove();
            nodeForm.refreshEntityNameIfTheSame(id);
            return true;
        }
        return false;
    }

}

let existingFiles = new ExistingFiles();