import static com.demo.ankush.Constants.MAVEN

def call(Map stageParams) {

    def maven = tool "${MAVEN}";
    print maven;
    sh "${maven}/bin/mvn ${stageParams.action}"
}