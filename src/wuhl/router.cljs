(ns wuhl.router
  (:require [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.guardrails.core :refer [>defn => | ? >def]]
            [goog.object :as g]
            [reitit.core :as r]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [wuhl.application :refer [SPA]]))

;; helpers ----------------------------------------------------------------------
(declare routes-by-name
         route-to!)

(defn map-vals [f m]
  (into {} (map (juxt key (comp f val))) m))

(defn make-routes-by-name
  "Returns a map like: {:root {:name :root :path '/'}}"
  [router]
  (let [grouped (group-by (comp :name second) (r/routes router))]
    (map-vals
      ;; takes the path string and adds it as the key :path
      (fn [[[path-str prop-map]]]
        (assoc prop-map :path path-str))
      grouped)))

;; routes ----------------------------------------------------------------------------
;; every routed component will munch its segment off of this using `route-segment`.
;; While the entire segment is listed for items that are nested below the top router,
;; it in fact will only use the last item in the sequence as its segment, so currently
;; components may not use a segment longer than one item, and the items before the last
;; item in the :segment vectors here are just for documentation.

(defn route-segment [name]
  (if-let [segment (some-> routes-by-name name :segment)]
    segment
    (throw (js/Error. (str "No matching fulcro segment for route: " (pr-str name))))))

(defn last-route-segment [name]
  (some-> (route-segment name) last vector))

(defn router-segment
  [name]
  (case name
    (ex-info "unknown router: " name)))

(def routes
  [["/"
    {:name    :home
     :segment [""]}]
   ])

(def router (rf/router routes {:data {:coercion rss/coercion}}))
(def routes-by-name (make-routes-by-name router))

;; keep track of where we are at the moment
(def current-fulcro-route* (atom []))
(defn current-fulcro-route [] @current-fulcro-route*)

(def redirect-loop-count (volatile! 0))
(def max-redirect-loop-count 10)

(defn on-match
  [SPA router m]
  (println "on-match called with: " (pr-str m))
  (let [m (or m {:path (g/get js/location "pathname")})
        {:keys [path]} m
        has-match? (rf/match-by-path router path)]
    (println "router, match: " (pr-str m))
    (if-not has-match?
      ;; unknown page, redirect to root
      (do
        (println "No fulcro route matched the current URL, changing to the default route.")
        (route-to! :home))

      (if-let [{:keys [route params]} (get-in m [:data :redirect-to])]
        ;; route has redirect
        (let [params (params)]
          (do (println "redirecting to: " route " with params " params)
              (if (> @redirect-loop-count max-redirect-loop-count)
                (do
                  (println (str "The route " route " hit the max redirects limit: " max-redirect-loop-count))
                  (vreset! redirect-loop-count 0))
                (do
                  (vswap! redirect-loop-count inc)
                  (js/setTimeout #(rfe/replace-state route params))))))

        ;;
        (let [path (:path m)
              segments (->> (clojure.string/split path "/" -1) (drop 1) vec)
              extended-segments (conj segments "")]
          (if (or (empty? segments)
                  (and (not (rf/match-by-path router path))
                       (rf/match-by-path router (str path "/"))))
            (do
              (println "Didn't find a match with " (pr-str segments)
                        ", using " (pr-str extended-segments) " instead")
              (reset! current-fulcro-route* extended-segments)
              (dr/change-route! SPA extended-segments))
            (do
              (println "Routing to " (pr-str segments))
              (reset! current-fulcro-route* segments)
              (dr/change-route! SPA segments))))))))

(defn current-route [this]
  (js/console.log (pr-str (dr/current-route this this)))
  (js/console.log (pr-str (first (dr/current-route this this))))
  (some-> (dr/current-route this this) first keyword))

(defn current-app-route []
  (dr/current-route SPA))

(defn current-route-from-url []
  (rf/match-by-path router (g/get js/location "pathname")))

(defn current-route-name
  "Returns the keyword name of the current route as determined by the URL path."
  []
  (some-> (current-route-from-url) :data :name))

(defn route=url?*
  [route-key params {{curr-name :name} :data curr-params :path-params}]
  (boolean
    (when-let [{:keys [name]} (routes-by-name route-key)]
      (and
        (= name curr-name)
        (= params curr-params)))))

(defn route=url?
  "predicate does the :key like :goals {:date \"2020-05-20\"}
  match current reitit match of the url"
  [route-key params]
  (route=url?* route-key params (current-route-from-url)))

(defn route-to!
  ([route-key]
   (route-to! route-key {}))

  ([route-key params]
   (let [{:keys [name] :as route} (get routes-by-name route-key)]
     (when-not (route=url? route-key params)
       (println "Changing route to: " (pr-str route))
       (rfe/push-state name params)))))

(defn redirect-to!
  "Like route-to!, but doesn't leave the current route in history"
  ([route-key]
   (redirect-to! route-key {}))

  ([route-key params]
   (let [{:keys [name] :as route} (get routes-by-name route-key)]
     (when-not (route=url? route-key params)
       (println "Redirecting to : " route)
       (rfe/replace-state name params)))))

(defn change-route-to-default! [this]
  (route-to! :home))

(defn change-route-after-signout! [this]
  (route-to! :home))

(defn route-for
  ([name]
   (route-for name {}))
  ([name params]
   (:path (rf/match-by-name router name params))))

(defn link
  ([route-name body]
   (link route-name {} body))
  ([route-name params body]
   (let [url (:path (rf/match-by-name router route-name params))]
     (dom/a :.item
            {:classes [(when (= route-name (current-route-name)) "active")]
             :href    url}
            body))))

(defn init! [SPA]
  (println "Starting router.")
  (dr/initialize! SPA)
  (rfe/start!
    router
    (partial on-match SPA router)
    {:use-fragment false}))


