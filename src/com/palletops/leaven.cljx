(ns com.palletops.leaven
  "A component composition library."
  (:require
   [com.palletops.leaven.protocols :as protocols
    :refer [ILifecycle IStatus]]))

(defn start
  "Start a component."
  [component]
  (if (satisfies? ILifecycle component)
    (protocols/start component)
    component))

(defn stop
  "Stop a component."
  [component]
  (if (satisfies? ILifecycle component)
    (protocols/stop component)
    component))

(defn status
  "Ask a component for its status."
  [component]
  (if (satisfies? IStatus component)
    (protocols/status component)))

#+clj
(defmacro defsystem
  "Macro to build a system defrecord out of `components`, a sequence
  of keywords that specify the sub-components.  The record will
  implement ILifecycle and IStatus by calling the protocol methods on
  each of the components.  The `start` method calls the sub-components
  in the specified order.  The `stop` method calls the sub-components
  in the reverse order."
  [record-name components]
  (letfn [(start-subcomp [k] `(update-in [~k] start))
          (stop-subcomp [k] `(update-in [~k] stop))
          (status-subcomp [k] `(update-in [~k] status))]
    `(defrecord ~record-name
         [~@(map (comp symbol name) components)]
       ILifecycle
       (~'start [component#]
         (-> component# ~@(map start-subcomp components)))
       (~'stop [component#]
         (-> component# ~@(map stop-subcomp (reverse components))))
       IStatus
       (~'status [component#]
         (-> component# ~@(map status-subcomp components))))))
