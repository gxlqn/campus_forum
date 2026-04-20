const app = getApp();

function getStaticBaseUrl() {
  return app.globalData?.staticBaseUrl || 'http://localhost:8080';
}

function getFullImageUrl(path, defaultImage) {
  const defaultImg = defaultImage || '/static/images/default-avatar.png';

  if (!path) return defaultImg;

  if (path.startsWith('/static/')) {
    return path;
  }

  if (path.startsWith('wxfile://') || path.startsWith('http://tmp/')) {
    return path;
  }

  if (path.startsWith('http://') || path.startsWith('https://')) {
    if (path.includes('localhost') || path.includes('127.0.0.1')) {
      const staticBaseUrl = getStaticBaseUrl();
      const uploadsIndex = path.indexOf('/uploads/');
      if (uploadsIndex !== -1) {
        return staticBaseUrl + path.substring(uploadsIndex);
      }
      const lastSlash = path.lastIndexOf('/');
      if (lastSlash !== -1) {
        return staticBaseUrl + '/uploads' + path.substring(lastSlash);
      }
      return path.replace(/http:\/\/(localhost|127\.0\.0\.1):\d+/, staticBaseUrl);
    }
    return path;
  }

  const staticBaseUrl = getStaticBaseUrl();
  let fullPath = path;
  if (!fullPath.startsWith('/')) {
    fullPath = '/' + fullPath;
  }
  if (!fullPath.includes('/uploads')) {
    fullPath = '/uploads' + fullPath;
  }
  return staticBaseUrl + fullPath;
}

function getImageList(images) {
  if (!images) return [];
  
  let imageArray = [];

  if (Array.isArray(images)) {
    imageArray = images;
  } else if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images);
      imageArray = Array.isArray(parsed) ? parsed : [images];
    } catch (e) {
      if (images.includes(',')) {
        imageArray = images.split(',').map(s => s.trim()).filter(Boolean);
      } else {
        imageArray = [images];
      }
    }
  }

  return imageArray
    .map(img => typeof img === 'string' ? img.trim() : null)
    .filter(img => img && img.length > 0)
    .map(img => getFullImageUrl(img));
}

function processUserAvatar(user) {
  if (!user) return '/static/images/default-avatar.png';
  return getFullImageUrl(user.avatar);
}

function processPostAuthor(post) {
  if (!post || !post.author) return post;
  return {
    ...post,
    author: {
      ...post.author,
      avatar: getFullImageUrl(post.author.avatar)
    }
  };
}

module.exports = {
  getFullImageUrl,
  getImageList,
  processUserAvatar,
  processPostAuthor,
  getStaticBaseUrl
};