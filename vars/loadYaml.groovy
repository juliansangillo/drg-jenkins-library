import org.yaml.snakeyaml.Yaml

def call(String file) {
    return new Yaml().load((file as File).text)
}
