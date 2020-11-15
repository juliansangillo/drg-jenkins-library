package com.deltarenegadegames.unityci

class Matrix {
    
    String prefix
    String[] axis
    Closure body

    def getTasks() {

        def tasks = [:]
        for(int i = 0; i < axis.size(); i++) {
            def axisValue = axis[i]
            def label = prefix + '-' + i
            tasks[axisValue] = {
                stage(axisValue) {
                    node(label) {
                        body()
                    }
                }
            }
        }

        return tasks
    }

}
