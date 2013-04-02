(ns totalrecloj.core)

(defprotocol TokenHandler
  (assoc-with-token [this token user-id password] "assoc's token with user and pass")
  (get-with-token [this token] "get's user and pass with token")
  (dissoc-token [this token] "dissoc's token from user and password"))

(defn generate-token-link 
  "create GET link that hits the endpoint at the host with the token as data"
  [{:keys [token host endpoint]}]
  (str host endpoint "?token=" (java.net.URLEncoder/encode token)))

(defn handle-postal-response [m]
  {:email-request (select-keys m [:code :message])})

(defn generate-token [] (java.util.UUID/randomUUID))

(defn email-token! [{:keys [email password host endpoint token-handler email-fn!]}]
  (let [token (str (generate-token))] 
    (do 
      (.assoc-with-token token-handler token email password)
      (email-fn! {:to email
                  :subject "Verify email"
                  :body (str "Click on the link to verify your email: " 
                             (generate-token-link {:token token
                                                   :host host
                                                   :endpoint endpoint}))}))))
