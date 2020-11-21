(ns wuhl.ui.step.column-config
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.ui.step.common :as common]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]))


(def ident [:component/id :column-config])

(defsc StepColumnConfig [this {:keys [data] :as props} {:keys [next-step last-step reset]}]
  {:query         [[:data '_]]
   :ident         (fn [] ident)
   :initial-state (fn [_] {})}

  ;; explanation
  (mui/card {}
    (mui/card-content {}
      (dom/p (tr lipsum))

      )

    (mui/card-actions {}
      (common/action-div
        {:back  (common/back-button {:onClick last-step})
         :next  (common/next-button {:onClick  next-step
                                     :disabled false})
         :reset (common/reset-button {:onClick reset})}))))

(def ui-step-column-config (c/factory StepColumnConfig))
