class UploadFile {

    constructor() {
        this.uploadedFile = document.getElementById("upload-file");
        this.uploadButton = document.getElementById("upload-button");
        document.getElementById("upload-file").value = null;
        this.uploadedFile.addEventListener("input", function () {
            uploadFileGlobal.uploadButton.disabled = uploadFileGlobal.uploadedFile.value === '';
        })
        this.uploadButton.disabled = this.uploadedFile.value === '' || this.uploadedFile.value === null;
    }

    async upload() {
        let formData = new FormData();
        formData.append("file", this.uploadedFile.files[0]);
        formData.append("source", this.uploadedFile.value);
        try {
            let response = await fetch('/thread/' + threadWrapper.getSelectedThread() + '/attachment', {
                method: "POST",
                headers: {
                    accept: 'application/json',
                },
                body: formData
            });
            if (response.status !== 200) {
                modalPopup.callPopup("File", "Uploading failed", false, () => {
                });
                return false;
            }
            this.uploadedFile.value = "";
            this.uploadButton.disabled = true;
            let returnedAttachment = await response.json();
            existingFiles.createNewThreadItem(returnedAttachment);
            modalPopup.callPopup("File", "File was successfully uploaded", false, () => {
            });
        } catch (e) {
            modalPopup.callPopup("File", "Error on file upload: " + e.message, false, () => {
            });
            return false;
        }
        return true;
    }
}

let uploadFileGlobal = new UploadFile();