import express from 'express';
import bodyParser from 'body-parser'
import axios from 'axios';
import cors from 'cors';
import path from 'path';
const app = express();
const port = 3000;
const __dirname = path.resolve();

app.set("view engine", "pug");
app.set("views", path.join(__dirname, "views"));

app.use(cors());

// Configuring body parser middleware
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());


app.post('/info', async (req, res) => {
    const key = req.body.userKey;
    const uuid = await sendRequest('https://api.hypixel.net/key', {key: key})
        .then(result => {
            return result.record.owner;
        })
        .catch(error => {
            console.log(error.response.data.cause);
            res.render("index2", {message: error.response.data.cause});
            return false;
        });
    if(uuid){
        return;
    }
    const gamesPromise = sendRequest('https://api.hypixel.net/resources/games', {key: key}).catch(error => {
        res.render("index2", {message: error.response.data.cause});
    });
    const playerPromise = sendRequest('https://api.hypixel.net/player', {key: key, uuid: uuid }).catch(error => {
        res.render("index2", {message: error.response.data.cause});
    });
    const playerStatusPromise = sendRequest('https://api.hypixel.net/status', {key: key, uuid: uuid }).catch(error => {
        res.render("index2", {message: error.response.data.cause});
    });
    const recentGamesPromise = sendRequest('https://api.hypixel.net/recentgames', {key: key, uuid: uuid }).catch(error => {
        res.render("index2", {message: error.response.data.cause});
    });
    const friendsPromise = await sendRequest('https://api.hypixel.net/friends', {key: key, uuid: uuid }).catch(error => {
        res.render("index2", {message: error.response.data.cause});
    });
    const friendsList = await getFriendsList(friendsPromise, key);

    const data = await Promise.all([gamesPromise,playerPromise, playerStatusPromise, recentGamesPromise])
        .then(([gamesPromise, playerPromise, playerStatusPromise, recentGamesPromise]) => {
            const hypixelGames = getHypixelGames(gamesPromise.games);
            const playerName = getPlayerName(playerPromise.player);
            const playerRank = getPlayerRank(playerPromise.player);
            const playerStatus = getPlayerStatus(playerStatusPromise);
            const recentPlayerGames = getRecentPlayerGames(recentGamesPromise.games);
            return {
                'hypixelGames': hypixelGames,
                'playerName': playerName,
                'playerRank': playerRank,
                'playerStatus': playerStatus,
                'recentPlayerGames': recentPlayerGames,
            }
        })
        .catch(error => {
            console.log(error);
        });

    data['friendsList'] = friendsList;
    res.render("index", {data: data});
});

app.listen(port, () => {
    console.log(`Example app listening at http://localhost:${port}`);
});

async function sendRequest(url, params) {
    return await axios.get(url, {
        params: params
    }).then(result => {
        return result.data;
    }).catch(error => {
        throw error;
    })
}

async function getFriendsList(friendsPromise, key) {
    const result = await Promise.all(friendsPromise.records.map(record => {
        return getPlayerNameByUUID(record.uuidReceiver, key);
    }));
    return result;
}

async function getPlayerNameByUUID(uuid, key) {
    const tmp = await sendRequest('https://api.hypixel.net/player', {key: key, uuid: uuid })
        .then(result => {
            return getPlayerName(result.player);
        });
    return tmp;
}

function getHypixelGames(games){
    const result = [];
    Object.keys(games).forEach(function(key) {
        result.push(games[key].name);
    });
    return result;
}

function getPlayerName(player) {
    return player.displayname;
}

function getPlayerRank(player) {
    return player.newPackageRank;
}

function getPlayerStatus(status) {
    return status.session.online;
}

function getRecentPlayerGames(games) {
    const result = [];
    for(let game in games) {
        result.push({
            date: game.date,
            gameType: game.gametype,
        });
    }
    return result;
}