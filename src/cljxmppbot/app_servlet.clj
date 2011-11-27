(ns cljxmppbot.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use cljxmppbot.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method cljxmppbot-app) this request response))
