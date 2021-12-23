sleep 10

rs.initiate(
    {_id: "rs1", version: 1,
        members: [
            { _id: 0, host : "mongo_1:27017" },
            { _id: 1, host : "mongo_2:27017" }
        ]
    }
);

db.person.insertMany([
    {_id: "a", firstName: "Gregory", lastName: "Peck", age: 40, role: "CEO", appraisals: 1, salary: 1200000, childrenIds: ["b", "c"], email: "a@corp.com"},

    {_id: "b", firstName: "Adam", lastName: "Sandler", age: 50, role: "IT_MANAGER", appraisals: 0, salary: 95000, parentId: "a", childrenIds: ["d", "e"], email: "b@corp.com"},
    {_id: "c", firstName: "Meg", lastName: "Ryan", age: 60, role: "HR_MANAGER", appraisals: 2, salary: 100000, parentId: "a", childrenIds: ["f", "g"], email: "c@corp.com"},

    {_id: "d", firstName: "James", lastName: "Bond", age: 35, role: "IT", appraisals: 0, salary: 55000, parentId: "b", email: "d@corp.com"},
    {_id: "e", firstName: "Monica", lastName: "Belluci", age: 21, role: "IT", appraisals: 0, salary: 42000, parentId: "b", email: "e@corp.com"},
    {_id: "f", firstName: "Frank", lastName: "Moody", age: 27, role: "HR", appraisals: 0, salary: 510000, parentId: "c", email: "f@corp.com"},
    {_id: "g", firstName: "Frank", lastName: "Sinatra", age: 32, role: "HR", appraisals: 1, salary: 60000, parentId: "c", email: "g@corp.com"}
])

db.person.createIndex({"firstName": 1, "lastName": 1})

db.stats()
