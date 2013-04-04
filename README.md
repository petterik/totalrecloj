# totalrecloj

A Clojure library designed to handle the work flow of sending an email for user verification.

## The name

The name is a Clojure twist on the movie title Total Recall, where the proccess of going to an email and comming back to the site for verification is supposed to be the "Recall".

## Usage

1. Implement the two protocolls TokenHandler and UserPersister.
2. Call `email-token!`
```clojure
(email-token! {:email "user-email@domain.com"
                   :password "users-requested-password"
                   :host "url.to.your.site.com"
                   :endpoint "/verification/endpoint"
                   :token-handler token-handler-impl
                   :email-fn! email-fn-one-map-argument})
```

3. Let the verification endpoint call `verify-token!` with token.
```clojure
(verify-token! {:token token
                    :token-handler token-handler-impl
                    :user-persister user-persister-impl})
```

Example of email-fn! with postal:
```clojure
(ns recloj.example
  (:require [postal.core :as mail]))

(defn send-email! [{:keys [to body subject]}]
  (mail/send-message ^{:host "smtp.gmail.com"
                       :user "gmail.user"
                       :pass "passw0rd"
                       :ssl :yes-please}
                      {:from "gmail.user@gmail.com"
                       :to to
                       :subject subject
                       :body body}))
```

## License

Copyright © 2013 Splunk

Distributed under the Eclipse Public License, the same as Clojure.

