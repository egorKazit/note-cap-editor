class ModalPopup {

    constructor() {
        this.confirmCallback = {};
        this.closeCallback = {};
        this.title = document.getElementById("modal-popup-header");
        this.text = document.getElementById("modal-popup-text");
        this.popupDiv = document.getElementById("modal-popup");
        this.popup = new bootstrap.Modal(this.popupDiv);
        $("#modal-popup").on("hidden.bs.modal", function () {
            modalPopup.closeCallback();
        });
    }

    callPopup(title, text, isConfirmation, callback) {
        this.title.innerText = title;
        this.text.innerText = text;
        if (isConfirmation) {
            document.getElementById("modal-popup-confirm").hidden = false;
            this.confirmCallback = callback;
            this.closeCallback = {};
        } else {
            document.getElementById("modal-popup-confirm").hidden = true;
            this.confirmCallback = {};
            this.closeCallback = callback;
        }
        this.popup.show();
    }

    confirm() {
        this.confirmCallback();
        this.popup.hide();
    }

}

let modalPopup = new ModalPopup()