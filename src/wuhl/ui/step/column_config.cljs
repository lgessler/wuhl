(ns wuhl.ui.step.column-config
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.ui.step.common :as common]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]
            [wuhl.models.columns :as wc]))

;; individual column component --------------------------------------------------------------------------------
(defmutation change-type [{:keys [new-type]}]
  (action [{:keys [state component]}]
    (m/set-value! component :column/type new-type)))


(defsc Column [this {:column/keys [id type options]}]
  {:query [:column/id :column/type :column/options]
   :ident :column/id}
  (mui/grid {:container true :item true :key id}
    (mui/grid {:item true :sm 4}
      (mui/typography {:variant "body1"} id))
    (mui/grid {:item true :sm 4}
      (mui/form-control {}
                        ;; TODO: helptext inside here?
                        (mui/select {:onChange (fn [e]
                                                 (let [new-type (-> e .-target .-value keyword)]
                                                   (js/console.log new-type)
                                                   (c/transact! this [(change-type {:new-type new-type})])
                                                   ))
                                     :value    type}
                          (map (fn [{:keys [id name]}]
                                 (mui/menu-item {:value id :key id} name))
                               (vals wc/column-types)))))
    (mui/grid {:item true :sm 4}
      "Options")))

(def ui-column (c/factory Column))

;; preview component --------------------------------------------------------------------------------
(defn render-ast [{:keys [primary-form alternate-forms blocks]}]
  (mui/card {}
    (mui/card-header {:title primary-form})
    (mui/card-content {}
      (when (not-empty alternate-forms)
        (c/fragment
          (mui/typography {:variant "h6"} (tr "Alternate Forms"))
          (dom/ul {}
            (map #(dom/li {}
                    (mui/typography {:variant "body1"} %))
                 alternate-forms))))
      (for [{:keys [senses primary-lexcat lexcats]} blocks]
        (c/fragment
          (mui/typography {:variant "h6"} (if primary-lexcat
                                            (-> primary-lexcat
                                                (cond-> (not-empty lexcats) (str " (" (clojure.string/join ", " lexcats) ")")))
                                            (tr "Definition")))
          (dom/ul
            (map (fn [{:keys [body comments]}]
                   (dom/li {:key body} (cond-> body (not-empty comments) (str " (" (clojure.string/join ", " comments) ")"))))
                 senses)))))))

(defn preview [component column-configs rows]
  (let [types (set (map :column/type column-configs))]
    (c/fragment
      (mui/typography {:variant "h5" :component "h1"} "Preview")
      (if-let [error (wc/explain-config-error column-configs)]
        (mui/typography {:variant "body1"} error)
        ;; todo: let user select somehow
        (let [ast (first (wc/generate-ast column-configs rows))]
          (render-ast ast))))))


;; top level component --------------------------------------------------------------------------------
(def ident [:component/id :step-column-config])
(defsc StepColumnConfig [this {:keys [data columns] :as props} {:keys [next-step last-step reset]}]
  {:query         [[:data '_] {:columns (c/get-query Column)}]
   :ident         (fn [] ident)
   :initial-state {:columns []}}

  ;; explanation
  (let []
    (c/fragment
      (mui/typography {:variant "h5" :component "h1"} "Column Configuration")
      (dom/p (tr lipsum))
      (mui/container {}
        (mui/grid {:container true}
          (mui/grid {:item true :container true :md 8 :key "main"}
            (mui/grid {:container true :spacing 3}
              (mui/grid {:container true :item true}
                (mui/grid {:item true :sm 4 :key "cname"} (mui/typography {:variant "h6"} (tr "Column Name")))
                (mui/grid {:item true :sm 4 :key "ctype"} (mui/typography {:variant "h6"} (tr "Column Type")))
                (mui/grid {:item true :sm 4 :key "copts"} (mui/typography {:variant "h6"} (tr "Column Options"))))
              (map ui-column columns)))
          (mui/grid {:item true :md 4 :key "sidebar"}
            (preview this columns (:data data))
            )
          )
        )

      (common/action-div
        {:back  (common/back-button {:onClick last-step})
         :next  (common/next-button {:onClick  next-step
                                     :disabled false})
         :reset (common/reset-button {:onClick reset})}))))

(def ui-step-column-config (c/factory StepColumnConfig))
