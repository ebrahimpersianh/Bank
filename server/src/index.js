require('dotenv').config();
const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/auth');
const loansRoutes = require('./routes/loans');
const crashRoutes = require('./routes/crash');
const subscriptionRoutes = require('./routes/subscription');

const app = express();
app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => res.json({ ok: true }));
app.use('/api/auth', authRoutes);
app.use('/api/loans', loansRoutes);
app.use('/api/crash', crashRoutes);
app.use('/api/subscription', subscriptionRoutes);

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`سرور روی پورت ${port} بالا اومد`));
