# How to Run
- `java -Dspring.config.location=some/where/app.yml $JAVA_OPTS -jar build/libs/*.jar`
- `SPRING_CONFIG_LOCATION=some/where/app.yml java $JAVA_OPTS -jar build/libs/*.jar`
 
# Properties
yaml format
```yaml
model_server:
  hostname:
  username:
  password:
  interpreter_path:
  script_path:
  outdir_path:

client_origin:
wav_dir:
```

- `model_server.hostname`
- `model_server.username`
- `model_server.password`
- `model_server.interpreter_path`  
  ex) `/usr/bin/python3`
- `model_server.script_path` inference script
  ex) `/foo/bar/script.py`
- `model_server.outdir_path` inference output directory
- `client_origin` demo webpage origin
- `wav_dir`: directory for saving audio files (in this server)
