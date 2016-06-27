//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
var currentUser;
var targetUser;
webSocket.onopen = function(e) {
    console.log("Connection established!");
    var msg = {
        "sender" : getCurrentUser()
    };
    webSocket.send(JSON.stringify(msg));
};
webSocket.onclose = function () { alert("WebSocket connection closed") };
webSocket.onmessage = function (msg) {
    console.log(msg);
    updateChats(msg);
};

//Send message if "Send" is clicked
id("send").addEventListener("click", function () {
    sendChatMessage(id("message").value);
});

//Send message if enter is pressed in the input field
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendChatMessage(e.target.value); }
});

function registerOnClickForTarget() {
    var targetUserList = document.getElementsByClassName("targetUser");
    for (i = 0; i < targetUserList.length; i++) {
        targetUserList[i].onclick = function(e) {
            targetUser = e.target.textContent;
            id("chatTitle").innerHTML = "Chat records with " + targetUser;
            id("chats").innerHTML = "";
            var msg = {
              "history" : true,
              "sender" : getCurrentUser(),
              "receiver" : getTargetUser()
            };

            if (checkWebSocketAndAlert()) {
                return;
            }
            webSocket.send(JSON.stringify(msg));
        }
    }
}

//Send a chat message if it's not empty, then clear the input field.
function sendChatMessage(message) {
    if (checkWebSocketAndAlert()) {
        return;
    }
    if (message !== "") {
        var msg = {
            "sender" : getCurrentUser(),
            "receiver" : getTargetUser(),
            "message" : message
        };
        webSocket.send(JSON.stringify(msg));
        id("message").value = "";
    }
}

//Update the users list.
function updateChats(msg) {
    var data = JSON.parse(msg.data);
    if (data.history) {
        data.history.forEach(function (chat) {
            insert("chats", chat);
        })
        return;
    }
    // Handle chat message, only show if sender is self or targetUser.
    if (data.userMessage && (data.sender == targetUser || data.sender == currentUser)) {
        insert("chats", data.userMessage);
    }
    if (data.userlist) {
        id("userlist").innerHTML = "";
        data.userlist.forEach(function (user) {
            // Append an indicator for self.
            var userStr = user + (user === currentUser ? " (you)" : "");
            var liStr = user === currentUser
                ? "<li>" + userStr + "</li>"
                : "<li class='targetUser' style='cursor: pointer;'>" + userStr + "</li>";
            insert("userlist", liStr);
        });
        registerOnClickForTarget();
    }
    console.log(data);
}

function getCurrentUser() {
    if (currentUser) {
        return currentUser;
    }
    var currentUrl = window.location.href.split("/");
    currentUser = currentUrl[currentUrl.length - 1];
    return currentUser;
}

function getTargetUser() {
    return targetUser;
}

// Alert and return true if socket is closed, otherwise return false;
function checkWebSocketAndAlert() {
    if(webSocket.readyState === webSocket.CLOSED){
        alert("WebSocket connection closed, please refresh page.");
        return true;
    }
    return false;
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}
