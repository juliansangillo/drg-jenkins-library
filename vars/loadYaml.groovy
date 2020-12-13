@Grab('org.yaml:snakeyaml:1.17')

import org.yaml.snakeyaml.Yaml

def call(String file) {
    return new Yaml().load((file as File).text)
}
