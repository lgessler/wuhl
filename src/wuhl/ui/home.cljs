(ns wuhl.ui.home
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.router :as r]
            [wuhl.i18n :refer [tr lipsum]]
            [wuhl.ui.step.upload :refer [ui-step-upload StepUpload]]))

;; At a high level: user will upload data and go through some steps configuring it
;; The data will be stored in the :data root key of the state atom

(def ident [:component/id :home])

(defmutation reset-steps [_]
  (action [{:keys [state]}]
          (swap! state #(assoc-in % (conj ident :active-step) 0))
          (swap! state :data dissoc)))

(defsc Home [this {:keys [active-step
                          step-upload] :as props}]
  {:query         [:active-step
                   {:step-upload (c/get-query StepUpload)}]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:active-step 0
                           :step-upload (c/get-initial-state StepUpload)})
   :route-segment (r/last-route-segment :home)}

  (let [advance #(m/set-integer! this :active-step (inc active-step))
        reset #(c/transact! this [(reset-steps {})])]
    (mui/page-container {:style {:maxWidth "800px"}}
      (c/fragment
        (mui/stepper {:activeStep  active-step
                      :orientation "vertical"}
          (mui/step {:key 0}
            (mui/step-label {} (tr "Upload Tabular Data"))
            (mui/step-content {}
              (ui-step-upload
                (c/computed step-upload {:advance advance :reset reset}))))
          (mui/step {:key 1}
            (mui/step-label {} (tr "Configure Columns"))
            (mui/step-content {}
              (ui-step-upload
                (c/computed step-upload {:advance advance :reset reset}))))

          )))))

