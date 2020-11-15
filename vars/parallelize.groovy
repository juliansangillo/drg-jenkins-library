import com.deltarenegadegames.unityci.Matrix

def call(String labelPrefix, String[] axisValues, Closure execute) {
    
    def tasks = new Matrix(prefix: labelPrefix, axis: axisValues, body: execute).getTasks()
    
    parallel tasks
    
}
