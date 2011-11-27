(ns cljxmppbot.core
  (:require [appengine-magic.core :as ae]
	    [appengine-magic.services.task-queues :as tq]
	    [compojure.response :as response]
	    [compojure.route :as route]
	    )
  (:use
   compojure.core
   [clojure.string :only [capitalize]]
   [clojure.contrib.seq-utils :only [find-first]]
   [appengine-magic.multipart-params :only [wrap-multipart-params]]
   [ring.middleware params keyword-params]
   [hiccup.core]
   [hiccup.page-helpers]
   [hiccup.form-helpers])
  (:import com.google.appengine.api.xmpp.JID)
  (:import com.google.appengine.api.xmpp.Message)
  (:import com.google.appengine.api.xmpp.MessageBuilder)
  (:import com.google.appengine.api.xmpp.SendResponse)
  (:import com.google.appengine.api.xmpp.XMPPService)
  (:import com.google.appengine.api.xmpp.XMPPServiceFactory))


(def xmpp (XMPPServiceFactory/getXMPPService))
(def *server-name* "bot@cljxmppbot.appspotchat.com")

;; Predicats
(defn not-ascii? [s]
  (not (re-matches #"\p{ASCII}*" s)))

(defn foul-word? [s]
  (not (nil? (re-matches #".*fuck.*" s))))

(defn question? [s]
  (not (nil? (re-matches #".*\?" s))))

(defn greeting? [s]
  (= "hi" s))

;;;;

(defn msg-bld [from tos body]
  (let [bld (MessageBuilder.)]
    (doto bld
      (.withRecipientJids (into-array JID (map #(JID. %) tos)))
      (.withFromJid (JID. from))
      (.withBody body))
    (.build bld)))

(defn send-msg [msg]
  (try
    (let [to (-> msg .getRecipientJids first)
	  status (.sendMessage xmpp msg)
	  stat (.getStatusMap status)
	  ]
      stat)
    (catch Exception e (str "send dail:" e))))

(defn send-res [to body]
  (println "send-res" to body)
  (send-msg (msg-bld *server-name* [to] body)))
	    
(defn form [req]
  "generate the main HTML frontend"
  (html
   (doctype :html4)
   [:head 
    [:h1 "XMPP Bot test page v1i"]
    ]
   [:body
    [:h3 "to interact with the bot, just add the following to your google talk or any other XMPP client: bot@cljxmppbot.appspotchat.com"]
    [:p]
    [:hr]
    [:p "Created by Tzach Livyatan as a demo app, without any guarantee what so ever"]
    [:p "Clojure Code is avilable on " (link-to "https://github.com/tzach" "github")]]
    ))

(defn handle-queue-msg [from to body]
  (println "handle-queue-msg. from:" from ",to:" to "body:" body)
  (send-res from (str body "is a good question, but I do not have an answer yet."))
  (send-res from "I'm still learning, please come back in a few days.")
  )
  

;; map of a predicat, action  and true and false response
(def *pred-res*
     {not-ascii? [:send "Sorry, I only speak plain English. please try to rephrase"]
      greeting? [:send "Hello to you to!"]
      foul-word? [:send "Sorry, unlike you, I will not ue or allow foul words"]
      question? [:delay "Interesting question! Let me think for a few seconds..."]
      identity [:send "ask me anything"]})
     

(defn handle-chat-msg [from to body]
  (println "handle-chat-msg. from:" from ",to:" to "body:" body)
  (let [s (-> body clojure.string/lower-case clojure.string/trim)
	[pred [action res]] (find-first #((first %) s) *pred-res*)]
    (condp  = action
	:send (send-res from res)
	:delay (do
		 (send-res from res)
		 (tq/add! :url "/chat_callback/"
			  :params {:body body :from from :to to}
			  :countdown-ms 20000)))))

(defroutes cljxmppbot-app-handler
  (GET "/" [] form)
  (POST "/_ah/xmpp/message/chat/" [body from to]
	(handle-chat-msg from to body)
	{:status 200})
  (POST "/chat_callback/" [body from to]
	(do
	  (handle-queue-msg from to body)
	  {:status 200}))
  (POST "/_ah/xmpp/presence/available/" [] {:status 200})
  (POST "/_ah/xmpp/presence/probe/" [] {:status 200})
  (route/resources "/")
  (route/not-found "Page not found"))


(ae/def-appengine-app cljxmppbot-app (-> #'cljxmppbot-app-handler
					 wrap-multipart-params
					 wrap-keyword-params
					 wrap-params))




;; (ae/start cljxmppbot-app)