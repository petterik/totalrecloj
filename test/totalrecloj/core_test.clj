(ns totalrecloj.core_test
  (:use midje.sweet
        totalrecloj.core))

(facts
  "generate-token-link"
  (fact (generate-token-link {:token "ma-token"
                              :host "http://www.example.com"
                              :endpoint "/endpoint"})
        => "http://www.example.com/endpoint?token=ma-token")  
  (fact "token is url encoded"
        (generate-token-link {:token "with-special-chars-?/"
                              :host "http://www.example.com"
                              :endpoint "/endpoint"})
        => (str "http://www.example.com/endpoint?token=" 
                (java.net.URLEncoder/encode "with-special-chars-?/"))))

(facts
  "handle-postal-response"
  (fact "keep message and code"
        (handle-postal-response {:code 0
                                 :error :SUCCESS
                                 :message "yay"}) => {:email-request {:code 0
                                                                      :message "yay"}}))

(fact "generate-token is a java.util.UUID for uniqueness"
      (class (generate-token)) => java.util.UUID)

(facts 
  "sending token email"
  (fact "assoc's token with user and pass"
        (let [mem (atom {})
              assocer (reify TokenHandler 
                        (assoc-with-token [this token user pass]
                          (reset! mem {:token token :user user :password pass})))]
          (email-token! {:email "user@domain.com" 
                         :password "pass"
                         :token-handler assocer
                         :email-fn! (fn [_] nil)})
          (:user @mem) => "user@domain.com"
          (:password @mem) => "pass"
          (-> (:token @mem) frequencies (get \-)) => 4)) ;UUID's have 4 '-' chars
  (fact "sends the right things to the email fn"
        (let [mem (atom {})
              email-fn (fn [m] (reset! mem m))]
          (email-token! {:email "user@domain.com"
                         :host "host.com"
                         :endpoint "/endpoint"
                         :token-handler (reify TokenHandler (assoc-with-token [a b c d] nil))
                         :email-fn! email-fn})
          (:to @mem) => "user@domain.com"
          (:subject @mem) => "Verify email"
          (:body @mem) => #".*host\.com/endpoint\?token=.*")))

