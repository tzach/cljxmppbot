(defproject cljxmppbot "1.0.0-SNAPSHOT"
  :description "A GAE XMPP base boot"
  :dependencies [[org.clojure/clojure "1.2.1"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [compojure "0.6.4"]
		 [hiccup "0.3.7"]
		 ;;		 [clojail "0.5.0"]
		 ]
  :dev-dependencies [[appengine-magic "0.4.6-SNAPSHOT"]
		     [swank-clojure "1.3.3"]]
  )


;; http://github.com/gcv/appengine-magic

;; run localy
;; lein appengine-clean
;; lein appengine-prepare


;; 3. d:/code/appengine-java-sdk-1.5.5/bin/dev_appserver.cmd war/

;; deploy on GAE
;; d:/code/appengine-java-sdk-1.5.5/bin/appcfg.cmd update war/

