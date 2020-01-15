const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.changeStatus = functions.https.onRequest(async (req, res) => {
    try {
        if (req.method !== 'POST')
            return res.status(405).send('Method not allowed');

        const body = req.body;
        if (!body.labId && !body.devId)
            return res.status(400).send('Bad request');

        var ref = admin.database().ref(`/labs/${body.labId}/devices/${body.devId}`);
        let snapshot = await ref.once('value');
        if (snapshot.exists()) {
            switch (snapshot.child('state').val()) {
                case 'Available':
                    await snapshot.child('state').ref.set('Moved');

                    var labRef = admin.database().ref(`/labs/${body.labId}`);
                    var labSnapshot = await labRef.once('value'); 

                    var tokensSnapshot = await admin.database()
                        .ref(`/tokens`).once('value');
                    var tokens = Object.keys(tokensSnapshot.val());
                    var payload = {
                        notification: {
                            title: 'Dispositivo movido',
                            body: `${snapshot.child('name').val()} del ${labSnapshot.child('name').val()} fue retirado sin permiso`
                        }
                    };
                    await admin.messaging().sendToDevice(tokens, payload);

                    break;
                case 'Borrowed':
                    //change it to available...
                    break;
                case 'Moved':
                    //... 
                    break;
            }
            return res.status(200).send('Ok');
        } else {
            return res.status(404).send('Not found');
        }
    } catch (err) {
        console.log(err);
        return res.status(500).send('Internal server error');
    }
});