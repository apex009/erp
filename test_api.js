const http = require('http');

const url = 'http://127.0.0.1:9090/api/reports/sales/day?start=2026-01-26&end=2026-02-26';

http.get(url, (res) => {
  let data = '';
  res.on('data', (chunk) => {
    data += chunk;
  });
  res.on('end', () => {
    console.log('STATUS:', res.statusCode);
    console.log('BODY:', data);
  });
}).on('error', (err) => {
  console.log('ERROR:', err.message);
});
