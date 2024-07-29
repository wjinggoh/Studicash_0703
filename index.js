const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cron = require('node-cron');

admin.initializeApp();

exports.scheduleTransactions = functions.pubsub.schedule('0 0 * * *').onRun(async (context) => {
    const goalsSnapshot = await admin.firestore().collection('Goal').get();
    const currentDate = new Date().toISOString().split('T')[0]; // Get current date in YYYY-MM-DD format

    goalsSnapshot.forEach(async (goalDoc) => {
        const goal = goalDoc.data();
        const startDate = new Date(goal.startDate);
        const endDate = new Date(goal.endDate);
        const savingsNeeded = goal.savingsNeeded;
        const frequency = goal.savingFrequency;

        if (currentDate >= goal.startDate && currentDate <= goal.endDate) {
            const transactionData = {
                date: currentDate,
                category: goal.name,
                amount: savingsNeeded,
            };

            await admin.firestore().collection('expenseTransactions').add(transactionData);
        }
    });

    return null;
});
