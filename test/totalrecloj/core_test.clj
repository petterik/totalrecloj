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

(facts
  "verifying token"
  (fact "takes user assoc'ed with token and persist the user as a verified user"
        (let [tok "token"
              user {:email "email@domain.com"
                    :password "pass"}
              mem (atom {:token tok})
              token-handler (reify TokenHandler
                              (get-with-token [this token] 
                                (when (= tok token) user))
                              (dissoc-token [this token] 
                                (when (= tok token) (swap! mem dissoc :token))))]
          (-> (verify-token! {:token tok
                              :token-handler token-handler
                              :user-persister 
                              (reify UserPersister
                                (persist-verified-user [this email pass] 
                                  (swap! mem assoc :email email :password pass)))})
              :message) => #".*verified user.*" 
          (:token @mem) => nil 
          (:email @mem) => (:email user) 
          (:password @mem) => (:password user)))
  (fact "un-assoc'ed token -> 'invalid token' message"
        (-> (verify-token! {:token "token"
                            :token-handler (reify TokenHandler 
                                             (get-with-token [this token] nil))
                            :user-persister nil})
            :message) => #".*[Ii]nvalid token.*token.*"))

