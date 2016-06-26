import controller.WebSocketsController;

import static j2html.TagCreator.article;
import static j2html.TagCreator.body;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.link;
import static j2html.TagCreator.p;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;
import static j2html.TagCreator.ul;
import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

/**
 * The entry point of the web app. Run the main method to start the app.
 */
public class ChatBox {

    public static void main(String[] args) {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(600);
        webSocket("/chat", WebSocketsController.class);
        post("/username", (req, res) -> {
            String username = req.queryParams("value");
            res.redirect("/chats/" + username);
            return null;
        });
        get("/chats/:username", (req, res) -> {
            return html().with(
                head().with(title("Chat Box"), link().withRel("stylesheet").withHref("/style.css")),
                body().with(
                    div().with(
                        input().withId("message").withPlaceholder("Type your message.."),
                        button("Send").withId("send")
                    ),
                    article()
                        .with(p("Select a user in the right panel to chat with.")
                        .withId("chatTitle"),
                    ul().withId("userlist"), ul().withId("chats")),
                    script().attr("src", "/chats.js"))).render();
        });
        init();
    }
}
