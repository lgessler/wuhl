(ns wuhl.ui.home
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.router :as r]
            [wuhl.i18n :refer [tr lipsum]]
            [wuhl.ui.step.upload :as step-upload :refer [ui-step-upload StepUpload]]
            [wuhl.ui.step.column-config :as step-column-config :refer [ui-step-column-config StepColumnConfig]]))

;; At a high level: user will upload data and go through some steps configuring it
;; The data will be stored in the :data root key of the state atom

(def ident [:component/id :home])

(defmutation reset-steps [_]
  (action [{:keys [state]}]
    (swap! state #(assoc-in % (conj ident :active-step) 0))
    (swap! state dissoc :data)
    (swap! state dissoc :column/id)
    (swap! state #(assoc-in % step-upload/ident (c/get-initial-state StepUpload)))
    (swap! state #(assoc-in % step-column-config/ident (c/get-initial-state StepColumnConfig)))
    ))

(defsc Home [this {:keys [active-step
                          step-upload
                          step-column-config] :as props}]
  {:query         [:active-step
                   {:step-upload (c/get-query StepUpload)}
                   {:step-column-config (c/get-query StepColumnConfig)}]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:active-step        0
                           :step-upload        (c/get-initial-state StepUpload)
                           :step-column-config (c/get-initial-state StepColumnConfig)})
   :route-segment (r/last-route-segment :home)}

  (let [next-step #(m/set-value! this :active-step (inc active-step))
        last-step #(m/set-value! this :active-step (dec active-step))
        reset #(c/transact! this [(reset-steps {})])
        step-div-style (fn [active-step i] (if (not= active-step i) {:display "none"} {}))]
    (mui/page-container {:style {:maxWidth "1000px"}}
      (c/fragment
        (mui/stepper {:activeStep  active-step
                      :orientation "horizontal"}
          (mui/step {:key 0}
            (mui/step-label {} (tr "Upload Tabular Data")))
          (mui/step {:key 1}
            (mui/step-label {} (tr "Configure Columns")))
          (mui/step {:key 2}
            (mui/step-label {} (tr "Language Information")))
          (mui/step {:key 3}
            (mui/step-label {} (tr "Download Zip"))))
        (dom/div {:style (step-div-style active-step 0)}
          (ui-step-upload
            (c/computed step-upload {:next-step next-step :reset reset})))
        (dom/div {:style (step-div-style active-step 1)}
          (ui-step-column-config
            (c/computed step-column-config {:next-step next-step :last-step last-step :reset reset})))
        (dom/div {:style (step-div-style active-step 2)}
          "NYI")
        (dom/div {:style (step-div-style active-step 3)}
          "NYI")
        ))))

