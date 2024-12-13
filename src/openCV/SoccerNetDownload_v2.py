from flask import Flask, jsonify
import SoccerNet
from SoccerNet.Downloader import SoccerNetDownloader

local_directory = "C:/work_oneteam/one-team-SA-proj/SoccerNet"
mySoccerNetDownloader = SoccerNetDownloader(LocalDirectory=local_directory)
mySoccerNetDownloader.password = "s0cc3rn3t"

app = Flask(__name__)

@app.route("/download", methods=["POST"])
def download_data():

    mySoccerNetDownloader.downloadGames(files=["1_224p.mkv", "2_224p.mkv"], split=["train","valid","test"])

    # download labels SN v2
    mySoccerNetDownloader.downloadGames(files=["Labels-v2.json"], split=["train","valid","test"])

    return jsonify({"message": "Download complete"})
    # 응답 반환

if __name__ == "__main__":
    app.run(debug=True)
