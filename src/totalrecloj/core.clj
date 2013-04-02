(ns totalrecloj.core)


(defn generate-token-link 
  "create GET link that hits the endpoint at the host with the token as data"
  [{:keys [token host endpoint]}]
  (str host endpoint "?token=" (java.net.URLEncoder/encode token)))

(defn handle-postal-response [m]
  {:email-request (select-keys m [:code :message])})

(defn generate-token [] (java.util.UUID/randomUUID))

(defn email-token! [{:keys [user password endpoint token-handler email-fn]}]
  (do 
    (.assoc-with-token token-handler (str (generate-token)) user password)
    nil))
