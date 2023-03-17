import static com.demo.ankush.Constants.GITHUB_CREDENTIALS_ID

def call(Map stageParams) {

    checkout([
        $class: 'GitSCM',
        branches: [[name:  stageParams.branch ]],
        userRemoteConfigs: [[ url: stageParams.url, credentialsId: "${GITHUB_CREDENTIALS_ID}",  ]]
        
    ])
  }