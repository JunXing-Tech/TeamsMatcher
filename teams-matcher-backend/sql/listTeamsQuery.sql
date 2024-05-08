SELECT
    id,
    name,
    description,
    maxNum,
    expireTime,
    userId,
    status,
    password,
    createTime,
    updateTime,
    isDelete
FROM team
WHERE isDelete = 0
  AND (
            id = ?
        AND name LIKE ?
        AND description LIKE ?
        AND (name LIKE ? OR description LIKE ?)
        AND maxNum = ?
        AND userId = ?
        AND status = ?
        AND (expireTime > ? OR expireTime IS NULL)
    )