class NoteForm {

    constructor() {
        this.entityName = document.getElementById("note-edit-name");
        this.entityValue = document.getElementById("note-edit-data");
        this.entitySaveButton = document.getElementById("note-edit-save");
        this.entityClearButton = document.getElementById("note-edit-clear");
        this.entityValue.addEventListener("keyup", (event) => {
            if (nodeForm.entityValue.value === "") {
                nodeForm.entityValue.style.borderColor = "red";
                nodeForm.controlButtons(true);
            } else {
                nodeForm.entityValue.style.borderColor = "";
                nodeForm.controlButtons(false);
            }
        })
        this.controlButtons(true);
    }

    clearForm() {
        this.entityName.value = "";
        if (this.entityValue.value === "")
            this.controlButtons(true);
    }

    async saveForm() {
        let requestUrl = "";
        let method;
        let isCreateOperation = false;
        if (this.entityName.value === "") {
            requestUrl = '/thread/' + threadWrapper.getSelectedThread() + '/note';
            method = 'POST';
            isCreateOperation = true;
        } else {
            requestUrl = "/thread/note/" + this.entityName.value;
            method = 'PUT';
        }
        try {
            let response = await fetch(requestUrl, {
                method: method,
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    'name': this.entityName.value,
                    "content": this.entityValue.value
                })
            });
            if (response.status !== 200) {
                modalPopup.callPopup("Note", "Error on note save. Server returned code " + response.status, false, () => {
                });
                return false;
            }
            let returnedNote = await response.json();
            if (isCreateOperation) {
                existingFiles.createNewThreadItem(returnedNote);
                this.entityName.value = returnedNote.ID;
            } else {
                document.getElementById("collapse-item-card-body-" + returnedNote.ID).innerText = returnedNote.content;
            }
        } catch (e) {
            modalPopup.callPopup("Note", "Error on note save: " + e.message, false, () => {
            });
            return false;
        }
        return true;
    }

    refreshEntityNameIfTheSame(id) {
        if (this.entityName.value === id) {
            this.entityName.value = "";
        }
    }

    controlButtons(isDisabled) {
        this.entitySaveButton.disabled = isDisabled;
        this.entityClearButton.disabled = isDisabled;
    }

}

let nodeForm = new NoteForm();